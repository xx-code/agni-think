FROM python:latest-alpine
# Installer les dépendances système nécessaires (build, ssl, etc)
RUN apk add --no-cache gcc musl-dev libffi-dev openssl-dev

# Créer et positionner le dossier de travail
WORKDIR /app

# Copier les fichiers requirements
COPY requirements.txt .

# Installer les dépendances python
RUN pip install --no-cache-dir -r requirements.txt

# Copier tout le code de l'app
COPY . .

# Exposer le port uvicorn (par défaut 8000)
EXPOSE 8000

# Commande de démarrage du serveur FastAPI avec uvicorn en mode production
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "4"]