# tz

A Clojure library designed to convert dates between time zones.

## Usage

Full:
`tz [yyMMdd] (hh)[:mm] [am|pm] (pst|cet|pdt|gmt+HH:MM) [pst|gmt:-HH:MM]`

Basic:
`tz <date?> <time> <am/pm?> <from-zone> <to-zone>`

Defaults to-zone to system time zone.

## License

Copyright © 2014 Axel Smeets

MIT THIS SHIT
