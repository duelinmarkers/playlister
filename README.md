# playlister

[KUTX](http://kutx.org) is an awesome radio station.
I felt like digging into their playlist.

KUTX sources its [public playlist](http://kutx.org/playlist) 
from an API hosted on `api.composer.nprstations.org`.
They provide interactive HTML documentation for quite a large API, though much of it
requires authentication, and I don't have credentials.
I'm using one of the endpoints that page uses to build a database of every play of
every song on KUTX and having fun with the data.


## Run It Yourself

Interface is only via the REPL, for now.

```
lein repl
```


## The Source API

Interactive HTML documentation is available at https://api.composer.nprstations.org/.

I'm using the [widget-day](https://api.composer.nprstations.org/#!/widget/Day_get_3)
endpoint to download all plays, one day at a time.

[`playlister.downloads`](https://github.com/duelinmarkers/playlister/blob/master/src/playlister/db.clj) 
hits the API and saves the JSON responses in the data directory.


## ETL

[`playlister.db`](https://github.com/duelinmarkers/playlister/blob/master/src/playlister/downloads.clj) 
creates a [sqlite](https://www.sqlite.org/) database and populates it from the JSON data files.


## Analysis

For now I'm just poking at the DB in the terminal.

```
sqlite3 data/plays.db
```

Any analysis can only be as good as the data. The data's a bit messy. 

* It only seems to go back to July of 2013.
* Some programs don't have playlists at all.
* Some programs have spotty playlists.
* Some programs have a bunch of duplicate plays. (I'm looking at you, Laurie Gallardo.)

I want to work around the duplicates before I start publishing any "results,"
but (with no corrections applied) here's a peek at the all-time (actually just mid-2013 through Q1-2016) 
top ten artists by play count.

1. Beck
2. Spoon
3. The Black Keys
4. Wilco
5. Tame Impala
6. Arcade Fire
7. The Beatles
8. David Bowie
9. Patty Griffin
10. My Morning Jacket


## License

Copyright © 2016 John D. Hume

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
