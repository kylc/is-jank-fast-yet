#!/usr/bin/env bash

set -eu

RUN=false
DRYRUN=false
PUBLISH=false
JANK_SOURCE="git+https://github.com/jank-lang/jank"

usage() {
	echo "Usage: $0 [OPTIONS]"
	echo "  --run           Run the benchmarks (default: false)"
	echo "  --dryrun        Do not append results to file, just print them"
	echo "  --publish       Publish to git (default: false)"
	echo "  --source <name> Specify the jank git source (default: git+https://github.com/jank-lang/jank)"
	echo "  --help          Display this help message"
	exit 1
}

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
	case $1 in
	--run)
		RUN=true
		shift
		;;
	--dryrun)
		DRYRUN=true
		shift
		;;
	--publish)
		PUBLISH=true
		shift
		;;
	--source)
		if [[ -n "$2" && "$2" != -* ]]; then
			JANK_SOURCE="$2"
			shift 2
		else
			echo "Error: --source requires a value."
			exit 1
		fi
		;;
	-h | --help)
		usage
		;;
	*)
		echo "Unknown option: $1"
		usage
		;;
	esac
done

export IJFY_DRYRUN="$DRYRUN"

export IJFY_CLJ_CMD="nix run --offline github:NixOS/nixpkgs/nixos-unstable#clojure --"
export IJFY_BB_CMD="nix run --offline github:NixOS/nixpkgs/nixos-unstable#babashka --"
export IJFY_JANK_CMD="nix run --offline $JANK_SOURCE --"

export IJFY_OUTPUT_DIR="gh-pages-checkout"
export IJFY_DATA_DIR="$IJFY_OUTPUT_DIR/data"
mkdir -p $IJFY_OUTPUT_DIR $IJFY_DATA_DIR

if [ "$RUN" = true ]; then
    # Refresh the runtime versions in our nix cache
    NIX_BUILD_FLAGS="--extra-substituters https://jank-lang.cachix.org \
        --extra-trusted-public-keys jank-lang.cachix.org-1:iLjYBD9b/v1D6FxUByF976x3BQ/AJGsQL1Rm49Sw7Fg="

    echo "Populating nix cache with language runtimes"
    nix build $NIX_BUILD_FLAGS --refresh \
        github:NixOS/nixpkgs/nixos-unstable#clojure \
        github:NixOS/nixpkgs/nixos-unstable#babashka \
        $JANK_SOURCE

	echo "Running bechmarks..."
	python3 harness.py
fi

if [ "$DRYRUN" = false ]; then
    echo "Rendering website to $IJFY_OUTPUT_DIR"
    quarto render index.qmd --output-dir $IJFY_OUTPUT_DIR --no-clean
fi

if [ "$PUBLISH" = true ]; then
	echo "Publishing to gh-pages"

	pushd $IJFY_OUTPUT_DIR
	git add -f index.html data/*.csv
	git commit -m "Update $(date -u)"
	git push origin gh-pages
	popd
fi
