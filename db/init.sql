CREATE TABLE public.gateway (
    id bigserial NOT NULL,
    serial_number varchar(255) NOT NULL,
    name varchar(255) NULL,
    ipv4 varchar(15) NOT NULL,
    CONSTRAINT gateway_pkey PRIMARY KEY (id),
    CONSTRAINT sn_unique UNIQUE (serial_number)
);

CREATE TABLE public.gateway_device (
   id bigserial NOT NULL,
   gateway_id int8 NULL,
   vendor varchar(255) NULL,
   status varchar(255) NULL,
   created_date timestamp NOT NULL DEFAULT timezone('utc'::text, now()),
   CONSTRAINT device_pkey PRIMARY KEY (id),
   CONSTRAINT fk_gateway_id FOREIGN KEY (gateway_id) REFERENCES public.gateway(id)
);