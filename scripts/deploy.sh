#!/bin/bash

ENV_FILE="./.env"

# Обновление кода и деплой backend приложения
pushd ~/IdeaProjects/FamilyChoresBot/ || exit

# Переходим на ветку main
git checkout main

# Обновляем ветку main
git pull origin main

# Останавливаем старые контейнеры микросервисов и запускаем новые, с обновлённым кодом
docker compose -f ~/IdeaProjects/FamilyChoresBot/docker-compose.yaml --env-file $ENV_FILE down --timeout=60 --remove-orphans
docker compose -f ~/IdeaProjects/FamilyChoresBot/docker-compose.yaml --env-file $ENV_FILE up --build --detach

popd || exit