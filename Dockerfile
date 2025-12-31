# Usa una imagen base de Ubuntu
FROM ubuntu:24.04

# Actualiza los paquetes e instala dependencias
RUN apt-get update && \
    apt-get dist-upgrade -yf &&\
    apt-get install -y openjdk-17-jdk \
                       tesseract-ocr \
                       tesseract-ocr-eng \
                       wget \
                       unzip \
                       curl \
                       cmake \
                       build-essential \
                       pkg-config \
                       libopencv-dev \
                       maven \
                       git && \
    apt-get clean

# Establece JAVA_HOME y el PATH
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Define el directorio de trabajo (aunque el código se montará con un volumen)
WORKDIR /app

# Expone el puerto de depuración
EXPOSE 5005

# Define el comando por defecto (puedes modificar esto en docker-compose)
CMD ["bash"]
