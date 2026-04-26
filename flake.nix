{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
    # jank.url = "git+https://github.com/jank-lang/jank?submodules=1";
  };

  outputs =
    inputs@{ flake-parts, ... }:
    flake-parts.lib.mkFlake { inherit inputs; } {
      systems = [
        "x86_64-linux"
        "aarch64-linux"
        "aarch64-darwin"
        "x86_64-darwin"
      ];
      perSystem =
        { pkgs, ... }:
        {
          devShells.default = pkgs.mkShell {
            packages = with pkgs; [
              nix

              uv
              ruff
              quarto

              python3
              python3Packages.numpy
              python3Packages.matplotlib
              python3Packages.plotly
              python3Packages.pandas
            ];
          };
        };
    };
}
