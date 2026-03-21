docker compose -p dind-jenkins  down
docker compose -p dind-jenkins  -f docker-compose.dind.yml up 

# Run this once on your VPS / host machine: #
docker run --privileged --rm tonistiigi/binfmt --install all


then dind container can use it wihtout install needed