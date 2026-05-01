# is-jank-fast-yet

Tracks relative runtime performance of Clojure dialects over time.

## Usage

Run the benchmark suite and produce an HTML output:

``` sh
./update.sh --run
```

Run using an alternative jank source:

``` sh
# Local checkout
./update.sh --run --source /path/to/jank-git

# Remote branch
./update.sh --run --source git+https://github.com/jank-lang/jank?ref=some-branch
```

Afterwards, you can check the output `index.html` page for graphical results.
