--
-- PostgreSQL database dump
--

\restrict 4jwXNFiYsOqyhM2ejp5svQn1FL9r8jD88c8sD0z8YmW0AqUkJl7h98OKqBA8KaB

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
-- Name: overtime_form; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.overtime_form (
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
    end_time timestamp(6) without time zone,
    reason character varying(255),
    start_time timestamp(6) without time zone
);


--
-- Data for Name: overtime_form; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.overtime_form (id, apply_time, approve_comment, approve_time, description, status, title, type, applicant_id, approver_id, end_time, reason, start_time) FROM stdin;
\.


--
-- Name: overtime_form overtime_form_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_form
    ADD CONSTRAINT overtime_form_pkey PRIMARY KEY (id);


--
-- Name: overtime_form fk9w3cyomsrm5x5t16s8r80tf3h; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_form
    ADD CONSTRAINT fk9w3cyomsrm5x5t16s8r80tf3h FOREIGN KEY (approver_id) REFERENCES public.users(id);


--
-- Name: overtime_form fky3j1ciorxegybim82k78tj15; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_form
    ADD CONSTRAINT fky3j1ciorxegybim82k78tj15 FOREIGN KEY (applicant_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

\unrestrict 4jwXNFiYsOqyhM2ejp5svQn1FL9r8jD88c8sD0z8YmW0AqUkJl7h98OKqBA8KaB

