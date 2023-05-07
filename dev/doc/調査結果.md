# spring boot 3 trace

目次
- [spring boot 3 trace](#spring-boot-3-trace)
  - [背景](#背景)
  - [全体感から見た各種ライブラリの役割](#全体感から見た各種ライブラリの役割)
    - [tracer layerの詳細](#tracer-layerの詳細)
    - [opentelemetryを使った場合のexporter layer詳細](#opentelemetryを使った場合のexporter-layer詳細)
    - [braveを使った場合のexporter layer(reporter)詳細](#braveを使った場合のexporter-layerreporter詳細)
  - [Opentelemetryに対応しているElastic APMに入れてみる](#opentelemetryに対応しているelastic-apmに入れてみる)
  - [参考資料](#参考資料)
    - [Traceについての公式ドキュメントの記述](#traceについての公式ドキュメントの記述)
    - [makingさんの最強資料](#makingさんの最強資料)

## 背景

spring boot3になってからtraceの構成が変わった。

sleuth から micrometer observationを使う用に代わり、opentelemetryが入ってきて関係性が変わったのでそれを把握したい。

micrometer observationの解説は優秀なブログがすでにあるのでそちらに任せることにする。

https://programacho.com/blog/a10b96ec9d79/ 

ここでは、このmicrometerの裏側を把握することを目的とする。

## 全体感から見た各種ライブラリの役割

▲ なにかのdiagramのルールにのっとって書いているわけではなく、雰囲気で矢印と図形の形を変えている。  
意味はあるが公のルールではないのでご注意。

![依存の関係性概要](./images/dependency-overview.drawio.svg)

### tracer layerの詳細

この領域はトレーサー層と呼ぶらしい。  
ここは、exporter（braveではreporter）を使って実際のトレース情報を伝送する部分の実装であったり、アプリケーション間のトレース情報をつなぐための処理を行う実装が存在する。

braveはもともとのspring cloudに存在する実装であり、zipkinのb3ヘッダのみに対応している。

opentelemetryでは、opentelemetryが定義する伝送プロトコル（なんだっけ？）とb3のどちらにも対応しており、ここをpropagatorと呼んでおり差し替え可能となっている。

- w3c → W3CTraceContextPropagator
- b3 → B3Propagator

![tracer layer](./images/tracer-layer-dependency.drawio.svg)

### opentelemetryを使った場合のexporter layer詳細

Opentelemetryに対応したトレース参照用のツールはいくつか存在する。

これ以外にも、上記zipkinの例から以下のような構造になっていることが伺える。

![exporter layer](./images/expoter-layer-dependency.drawio.svg)

参考資料のmakingさんの資料にある通り、spring boot 3.0.Xまでは、exporterのauto configに対応している（つまり何もBean登録しなくても設定だけで使えるやつ）のはzipkinとwavefrontのみとなっている。

しかし、opentelemetry自体はいろいろなexporterに対応しており、自力でBean登録をすることで異なるサービスにtraceを伝送することが可能になっている。

### braveを使った場合のexporter layer(reporter)詳細

tracerをopentelemetryではなく従来のbraveを選択することも可能となっている。  
この場合でも伝送先を切り替えることが可能となっている。

![reporter layer](./images/reporter-layer-dependency.drawio.svg)

## Opentelemetryに対応しているElastic APMに入れてみる

本リポジトリでは、前章でまとめた依存のうち以下のような組み合わせでアプリケーションを作成している。

- tracer layer -> opentelemetry
- exporter layer -> opentelemetry

tracerの記録先は Elastic APMを利用している。

詳しくは `../../pom.xml` を参照いただきたいが、traceにかかわる依存は以下の通り。

``` xml

		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-tracing-bridge-otel</artifactId>
		</dependency>

		<dependency>
			<groupId>io.opentelemetry</groupId>
			<artifactId>opentelemetry-exporter-otlp</artifactId>
		</dependency>

```

spring bootに関しては、3.0.x系であるため、そのままではopentelemetry exporterのauto configに対応していない。

このため、自分でBean登録を行った。

``` java
    @Bean
    SpanExporter otlpHttpSpanExporter() {
        return OtlpHttpSpanExporter.builder()
                .setEndpoint("http://localhost:8200/v1/traces") // <- elastic apm のエンドポイント
                .build();
    }
```

## 参考資料

### Traceについての公式ドキュメントの記述

https://docs.spring.io/spring-boot/docs/3.0.5/reference/htmlsingle/#actuator.micrometer-tracing

### makingさんの最強資料

https://bit.ly/springboot2023