--
-- PostgreSQL database dump
--

\restrict dEXWwRKJmtY6hfHYXp7eLNtIZb2G8ghOpNUQLVw8bpN9ukw0nGdqp8lQ6C1Dn6d

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: route_meta_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.route_meta_roles (
    meta_id bigint NOT NULL,
    role character varying(255)
);


--
-- Data for Name: route_meta_roles; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.route_meta_roles (meta_id, role) FROM stdin;
821	R_SUPER
821	R_ADMIN
822	R_SUPER
822	R_ADMIN
823	R_SUPER
823	R_ADMIN
800	R_SUPER
800	R_ADMIN
801	R_SUPER
801	R_ADMIN
802	R_SUPER
802	R_ADMIN
803	R_SUPER
803	R_ADMIN
804	R_SUPER
804	R_ADMIN
805	R_SUPER
805	R_ADMIN
806	R_SUPER
806	R_ADMIN
807	R_SUPER
807	R_ADMIN
807	R_USER
808	R_SUPER
808	R_ADMIN
809	R_SUPER
809	R_ADMIN
810	R_SUPER
810	R_ADMIN
811	R_SUPER
812	R_SUPER
812	R_ADMIN
812	R_USER
813	R_SUPER
813	R_ADMIN
813	R_USER
814	R_SUPER
814	R_ADMIN
814	R_USER
815	R_SUPER
815	R_ADMIN
815	R_USER
816	R_SUPER
816	R_ADMIN
817	R_SUPER
817	R_ADMIN
818	R_SUPER
818	R_ADMIN
818	R_USER
819	R_SUPER
819	R_ADMIN
820	R_SUPER
820	R_ADMIN
826	R_SUPER
826	R_ADMIN
\.


--
-- Name: route_meta_roles fkano9f2yoxxvuiegxkhgakgex1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.route_meta_roles
    ADD CONSTRAINT fkano9f2yoxxvuiegxkhgakgex1 FOREIGN KEY (meta_id) REFERENCES public.route_meta(id);


--
-- PostgreSQL database dump complete
--

\unrestrict dEXWwRKJmtY6hfHYXp7eLNtIZb2G8ghOpNUQLVw8bpN9ukw0nGdqp8lQ6C1Dn6d

