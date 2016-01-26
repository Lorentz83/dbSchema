select AL_NAME, AL_IATA_CODE, AL_ICAO_CODE
        F_ID, R_SEAT,
        F_DEPART_TIME, dap.AP_CODE as "depart airport",
        F_ARRIVE_TIME, aap.AP_CODE as "arrival airport"
from "RESERVATION" join "FLIGHT" on r_f_id = f_id
        join "AIRPORT" dap on dap.AP_ID = F_DEPART_AP_ID
        join "AIRPORT" aap on aap.AP_ID = F_ARRIVE_AP_ID
        join "AIRLINE" on F_AL_ID = AL_ID
where R_C_ID = 72057594037927949 AND dap.AP_CODE='ORD'
        AND F_DEPART_TIME > '2015-11-20' and F_DEPART_TIME < '2015-11-21';


select F_DEPART_TIME, dap.AP_CITY as "depart city", dap.AP_NAME as "depart airport", F_ARRIVE_TIME, aap.AP_CITY as "arrival city", aap.AP_NAME as "arrival airport"
from "RESERVATION" join "FLIGHT" on r_f_id = f_id
    join "AIRPORT" dap on dap.AP_ID = F_DEPART_AP_ID
    join "AIRPORT" aap on aap.AP_ID = F_ARRIVE_AP_ID
where R_C_ID = 72057594037927949;

select ap0.AP_NAME, ap0.AP_CODE, D_DISTANCE, ap1.AP_NAME, ap1.AP_CODE
from "AIRPORT_DISTANCE"
     join "AIRPORT" ap0 on ap0.AP_ID = D_AP_ID0
     join "AIRPORT" ap1 on ap1.AP_ID = D_AP_ID1
where
    ap0.AP_CITY = 'New York' and ap1.AP_CITY='Newburgh'
order by D_DISTANCE;

select * from "AIRPORT" where AP_CITY='New York';

select AP_CODE, F_DEPART_TIME, F_ARRIVE_TIME - F_DEPART_TIME as duration
from "AIRPORT" join "FLIGHT" on AP_ID=F_DEPART_AP_ID
where F_DEPART_TIME>'2015-11-09 11:00' and F_DEPART_TIME<'2015-11-09 12:30'
       and AP_CODE='ATL' and (F_ARRIVE_TIME - F_DEPART_TIME) > '1:00:00';

select AL_NAME,AL_IATA_CODE, AL_ICAO_CODE, F_ID, F_SEATS_LEFT ,F_SEATS_TOTAL, cast(F_SEATS_LEFT as float)/F_SEATS_TOTAL*100 as "free %"
from "FLIGHT" join "AIRLINE" on F_AL_ID=AL_ID
where cast(F_SEATS_LEFT as float)/F_SEATS_TOTAL > .5 and AL_IATA_CODE='QW';

select AL_NAME,AL_IATA_CODE, AL_ICAO_CODE, F_ID, F_SEATS_LEFT from "FLIGHT" join "AIRLINE" on F_AL_ID=AL_ID where F_SEATS_LEFT=0 and AL_IATA_CODE='QW';

select AP_CODE, AP_NAME, AP_CITY, F_ID from "FLIGHT" join "AIRPORT" on F_ARRIVE_AP_ID=AP_ID where F_SEATS_LEFT<10;

select AP_CODE, AP_NAME, AP_CITY, F_ID from "FLIGHT" join "AIRPORT" on F_DEPART_AP_ID=AP_ID where F_SEATS_LEFT<10;

select R_F_ID, count(C_IATTR02), count(C_IATTR03) from "RESERVATION" join "CUSTOMER" on R_C_ID=C_ID where R_F_ID = '336081684844052673' group by R_F_ID;

update "FREQUENT_FLYER" set ff_iattr00 = ff_iattr00 + 50
where FF_C_ID='281474976710668' and ff_al_id='651';

select a1.AP_NAME, F_ID, F_DEPART_TIME, a2.AP_NAME, F_ARRIVE_TIME as time
from "FLIGHT" join "AIRPORT" a1 on F_DEPART_AP_ID = a1.AP_ID
              join "AIRPORT" a2 on F_ARRIVE_AP_ID = a2.AP_ID
where a1.AP_CITY='Chicago' and a2.AP_CITY='Las Vegas';

select a1.AP_NAME, f1.F_ID, f1.F_DEPART_TIME, a2.AP_NAME, f2.F_ID, f2.F_ARRIVE_TIME,
      (f2.F_DEPART_TIME - f1.F_ARRIVE_TIME) as time
from "FLIGHT" f1 join "FLIGHT" f2 on f1.F_ARRIVE_AP_ID = f2.F_DEPART_AP_ID
      join "AIRPORT" a1 on f1.F_DEPART_AP_ID = a1.AP_ID
      join "AIRPORT" a2 on f2.F_ARRIVE_AP_ID = a2.AP_ID
where (f2.F_DEPART_TIME - f1.F_ARRIVE_TIME) between '1:00' AND '24:00'
      and a1.AP_CITY='Chicago' and a2.AP_CITY='Las Vegas'
order by time;

select AP_CITY, extract("month" from F_ARRIVE_TIME) as month, count(F_ID) as flights
from "FLIGHT" join "AIRPORT" on F_ARRIVE_AP_ID = AP_ID
group by AP_CITY, month
order by flights DESC;

select dco.CO_NAME as depart, aco.CO_NAME as arrive,
       extract("month" from F_ARRIVE_TIME) as month, count(F_ID) as flights
from "FLIGHT" join "AIRPORT" aap on F_ARRIVE_AP_ID = aap.AP_ID
      join "COUNTRY" aco on aap.AP_CO_ID=aco.CO_ID
      join "AIRPORT" dap on F_DEPART_AP_ID = dap.AP_ID
      join "COUNTRY" dco on dap.AP_CO_ID=dco.CO_ID
where aco.CO_ID<>dco.CO_ID
group by aco.CO_NAME, dco.CO_NAME, month
order by flights DESC;

select AL_NAME, sum(R_PRICE)
from "RESERVATION" join "FLIGHT" on R_F_ID=F_ID join "AIRLINE" on F_AL_ID=AL_ID
where AL_NAME='Federal Express'
      and F_DEPART_TIME > '2015-11-01' and F_DEPART_TIME < '2015-12-01'
group by AL_NAME;

update "FLIGHT" set F_SEATS_LEFT = (F_SEATS_LEFT - 1) where F_ID='2815514276397566';

update "FLIGHT" set F_STATUS=1 where F_ID='2815514276397566';