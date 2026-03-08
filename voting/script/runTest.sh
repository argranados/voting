# Este script asume que:
# ya existe la season
# ya existen los contestants cuyos IDs mandas en CONTESTANT_IDS

k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e SEASON_ID=1 \
  -e CONTESTANT_IDS=1,2 \
  script/voting-flow.js