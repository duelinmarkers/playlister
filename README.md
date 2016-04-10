# playlister

[KUTX](http://kutx.org) sources its [public playlist](http://kutx.org/playlist) 
from an API hosted on `api.composer.nprstations.org`.
They provide interactive HTML documentation for quite a large API, though much of it
requires authentication, and I don't have credentials.
I'm using one of the endpoints that page uses to build a database of every play of
every song on KUTX and having fun with the data.

## The Source API

Interactive HTML documentation is available at https://api.composer.nprstations.org/.

I'm using the [widget-day](https://api.composer.nprstations.org/#!/widget/Day_get_3)
endpoint to download all plays, one day at a time.

[`playlister.downloads`](https://github.com/duelinmarkers/playlister/blob/master/src/playlister/db.clj) 
hits the API and saves the JSON responses in the data directory.

## ETL

[`playlister.db`](https://github.com/duelinmarkers/playlister/blob/master/src/playlister/downloads.clj) 
creates a [sqlite](https://www.sqlite.org/) database and populates it from the JSON data files.

## Run It Yourself

Interface is only via the REPL, for now.

```
lein repl
```

## License

Copyright © 2016 John D. Hume

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
