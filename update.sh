#!/usr/bin/env bash

set -eux

NIX_FLAGS="--extra-substituters https://jank-lang.cachix.org \
    --extra-trusted-public-keys jank-lang.cachix.org-1:iLjYBD9b/v1D6FxUByF976x3BQ/AJGsQL1Rm49Sw7Fg="

nix build $NIX_FLAGS --refresh github:NixOS/nixpkgs/nixos-unstable#clojure
nix build $NIX_FLAGS --refresh github:NixOS/nixpkgs/nixos-unstable#babashka
nix build $NIX_FLAGS --refresh git+https://github.com/jank-lang/jank?submodules=1

export IJFY_CLJ_CMD="nix run --offline github:NixOS/nixpkgs/nixos-unstable#clojure --"
export IJFY_BB_CMD="nix run --offline github:NixOS/nixpkgs/nixos-unstable#babashka --"
export IJFY_JANK_CMD="nix run --offline git+https://github.com/jank-lang/jank --"

export IJFY_OUTPUT_DIR="gh-pages-checkout"
export IJFY_DATA_DIR="$IJFY_OUTPUT_DIR/data"

mkdir -p $IJFY_OUTPUT_DIR $IJFY_DATA_DIR
python3 harness.py
quarto render index.qmd --output-dir $IJFY_OUTPUT_DIR --no-clean

pushd $IJFY_OUTPUT_DIR
git add -f index.html data/*.csv
git commit -m "Update $(date -u)"
git push origin gh-pages
popd
