from github import Github
import mysql.connector

#specify your github token
token = "e069783d766ed1626fd722f910d42afa1607b9e6"
g = Github(token)

#specify file types you want to download
required_file_types = [".php"]

#specify the language you want to search github for
lang = "php"

#specify directory to store the unzipped projects
output_dir = "D:/Thesis/PHP_Mined/" 


#specify database credentials
mydb = mysql.connector.connect(
  host="localhost",
  user="root",
  passwd="",
  database="miner"
)