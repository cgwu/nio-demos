mvn exec:java

create a new repository on the command line

echo "# nio-demos" >> README.md
git init
git add README.md
git commit -m "first commit"
git remote add origin https://github.com/cgwu/nio-demos.git
git push -u origin master

…or push an existing repository from the command line

git remote add origin https://github.com/cgwu/nio-demos.git
git push -u origin master
