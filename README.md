#PostGIS test project

This project has been created to test out and play around with PostGIS.

As of right now this application creates a schema and populates the tables. After this you can play around with PostGis and earthdistance.

### Requirements
- sbt installed
- Safari browser (with allow remote automation enabled)
- a Postgres db on port 5432 call 'postgistest'(or edit application.conf to suit your needs)
- the cube, earthdistance and postgis extensions setup in your db

### Instructions
- make sure you meet all requirements mentioned above
- alter the code to test whatever you want
- sbt run

congrats! Your schema should now look as follows:

### Sequences
- location_id_seq
- person_id_seq

### Tables
 - location(id, point, geog)
 - person(id, name)
 - person_rel_location(person, location)