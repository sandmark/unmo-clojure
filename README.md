# Unmo - 形態素解析ベースの日本語チャットボット

Unmo は日本語チャットボットプログラムです。機械学習ではなく形態素解析に基づいてユーザーの発言を学習し、5 つの異なるエンジンが思考結果を返します。

- ホワット (単純に聞き返す)
- ランダム (学習したユーザーの発言からランダムに返す)
- パターン (名詞を学習し、発言に関係する言葉を返す)
- テンプレート (文章の名詞部分を他の名詞に置き換える)
- マルコフ (マルコフ連鎖アルゴリズムから独自の文章を生成する)

あくまでもユーザーの発言から文章を自動生成するだけなので、言葉の意味を理解しているわけではありません。そのため、乱暴な言葉ばかり使っていると乱暴なチャットボットに成長しますが、作者を恨まないでください。

はじめのうちはホワットやランダムが多いと感じるかもしれませんが、辛抱強く話しかけていると辞書にバリエーションが増え、あるときはまともな、あるときは突拍子もない反応をするようになります。じっくり話してあげてください。

## インストール
### 必要なもの
実行には JVM（Java 仮想マシン）が必要です。https://java.com/ja/download/ からダウンロード・インストールしてください。

また、形態素解析エンジンとして [Sudachi](https://github.com/WorksApplications/Sudachi/releases) を使用しています。辞書ファイルが必要になりますので、リンク先から `sudachi-0.1.1-dictionary-full.zip` をダウンロード・展開し、 `system_full.dic` を `unmo-x.x.x-standalone.jar` と同じディレクトリに置いてください。

Unmo 本体は https://github.com/sandmark/unmo-clojure/releases からダウンロードすることができます。

- `system_full.dic`
- `sudachi_fulldict.json`
- `unmo-x.x.x-standalone.jar`

上記 3 つのファイルが同じディレクトリにあれば準備完了です。

## 使い方

Unmo はコンソールアプリケーションであるため、コマンドを実行する必要があります。

Unix 系なら端末エミュレータ、Mac OS ならターミナル、Windows ならコマンドプロンプトを起動し、 `cd` コマンドで `unmo-x.x.x-standalone.jar` のあるディレクトリへ移動します。その後、

    $ java -jar unmo-0.1.0-standalone.jar
    
と打ち込んで Enter キーを叩けば起動します。

ひとしきり会話を楽しんだら、話しかけずに Enter キーを押せば終了します。

## アンインストール

`unmo-x.x.x-standalone.jar` のあるディレクトリを削除してください。

## 謝辞

このプログラムは Ruby 用の書籍 [恋するプログラム](https://amzn.to/2FAG0AA) を参考に制作されました。著者である秋山智俊さんに多大な感謝を申し上げます。

## License

Copyright © 2018-2019 sandmark

Distributed under the Eclipse Public License either version 1.0 or
any later version.
