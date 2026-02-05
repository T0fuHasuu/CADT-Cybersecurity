
- Create Folder Named **HelloWorld**
```bash
mdkir HelloWorld 
```

- Create File
```bash
echo "Print("Hello World")" > app.py
```

- Create **Dockerfile**
```bash
nano Dockerfile
```
	- Then Paste
		FROM python:3.11-slim
		WORKDIR /code
		COPY app.py /code/
		CMD ["python", "app.py"]
	- Then Ctrl+O, Enter and Ctrl+X

- Open Terminal In That Directory
```bash
docker build -t hello-python .
```

- Run Docker
```bash
docker run hello-python
```
