# import sqlite3

# conn = sqlite3.connect('courses.sqlite')

# c = conn.cursor()
# c.execute('''
#           CREATE TABLE courses
#           (id INTEGER PRIMARY KEY ASC, 
#            x INTEGER NOT NULL,
#            y INTEGER NOT NULL)
#           ''')

# conn.commit()
# conn.close()

# Lab 10 
import sqlite3

conn = sqlite3.connect('points.sqlite')

c = conn.cursor()
c.execute('''
          CREATE TABLE point
          (id INTEGER PRIMARY KEY ASC, 
           x INTEGER NOT NULL,
           y INTEGER NOT NULL)
          ''')

conn.commit()
conn.close()