FROM gradle:8.1.0-jdk17

WORKDIR /app
COPY . .
CMD ["gradle", "assembleDebug"]
