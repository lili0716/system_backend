--
-- PostgreSQL database dump
--

\restrict zJq9uJghgFaXBBLjpuRJYiqhp9IGPNdGrKB6WNtK8WbsBuMIk0wU8uUb2xFmTRL

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
-- Name: punch_card_form; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.punch_card_form (
    id bigint NOT NULL,
    apply_time timestamp(6) without time zone,
    approve_comment character varying(255),
    approve_time timestamp(6) without time zone,
    description character varying(255),
    status integer,
    title character varying(255),
    type integer,
    applicant_id bigint,
    approver_id bigint,
    punch_date timestamp(6) without time zone,
    punch_time timestamp(6) without time zone,
    punch_type integer,
    reason character varying(255),
    location character varying(255),
    abnormal_record_ids character varying(500)
);


--
-- Data for Name: punch_card_form; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.punch_card_form (id, apply_time, approve_comment, approve_time, description, status, title, type, applicant_id, approver_id, punch_date, punch_time, punch_type, reason, location, abnormal_record_ids) FROM stdin;
\.


--
-- Name: punch_card_form punch_card_form_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.punch_card_form
    ADD CONSTRAINT punch_card_form_pkey PRIMARY KEY (id);


--
-- Name: punch_card_form fk9w3cyomsrm5x5t16s8r80tf3h; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.punch_card_form
    ADD CONSTRAINT fk9w3cyomsrm5x5t16s8r80tf3h FOREIGN KEY (approver_id) REFERENCES public.users(id);


--
-- Name: punch_card_form fky3j1ciorxegybim82k78tj15; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.punch_card_form
    ADD CONSTRAINT fky3j1ciorxegybim82k78tj15 FOREIGN KEY (applicant_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

\unrestrict zJq9uJghgFaXBBLjpuRJYiqhp9IGPNdGrKB6WNtK8WbsBuMIk0wU8uUb2xFmTRL

