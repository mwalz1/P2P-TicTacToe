build:
	javac src/*.java

serve:
	cd src && java Main ${ARGS}

hot-reload:
	ls src/**/*.java src/**/*.html src/**/*.js src/**/*.css | entr -rs 'make && make serve'

clean:
	rm src/*.class
