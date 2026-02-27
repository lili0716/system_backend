--
-- PostgreSQL database dump
--

\restrict hQFCtX3J1ACEsQAPbtpL89pzbDtDaYS4bN2crrBw4IeS6YfdZAwZw7PXqLcqhFB

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
-- Name: department_routes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.department_routes (
    department_id bigint NOT NULL,
    route_id bigint NOT NULL
);


--
-- Data for Name: department_routes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.department_routes (department_id, route_id) FROM stdin;
\.


--
-- Name: department_routes fk611rll1j2ta40o0ncoiimsc3c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.department_routes
    ADD CONSTRAINT fk611rll1j2ta40o0ncoiimsc3c FOREIGN KEY (route_id) REFERENCES public.routes(id);


--
-- Name: department_routes fkof3is12btobgauaq8vwh9wo53; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.department_routes
    ADD CONSTRAINT fkof3is12btobgauaq8vwh9wo53 FOREIGN KEY (department_id) REFERENCES public.departments(id);


--
-- PostgreSQL database dump complete
--

\unrestrict hQFCtX3J1ACEsQAPbtpL89pzbDtDaYS4bN2crrBw4IeS6YfdZAwZw7PXqLcqhFB

