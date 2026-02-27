--
-- PostgreSQL database dump
--

\restrict Pjd5HANeavvw92lpVhzRj5ZvUaj9m0r83NDt0A3FKKxL08yBzeNkbXzccRxDek7

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
-- Name: field_work_form; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.field_work_form (
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
    content character varying(255),
    end_time timestamp(6) without time zone,
    location character varying(255),
    remark character varying(255),
    start_time timestamp(6) without time zone,
    work_date timestamp(6) without time zone
);


--
-- Data for Name: field_work_form; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.field_work_form (id, apply_time, approve_comment, approve_time, description, status, title, type, applicant_id, approver_id, content, end_time, location, remark, start_time, work_date) FROM stdin;
\.


--
-- Name: field_work_form field_work_form_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_work_form
    ADD CONSTRAINT field_work_form_pkey PRIMARY KEY (id);


--
-- Name: field_work_form fk9w3cyomsrm5x5t16s8r80tf3h; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_work_form
    ADD CONSTRAINT fk9w3cyomsrm5x5t16s8r80tf3h FOREIGN KEY (approver_id) REFERENCES public.users(id);


--
-- Name: field_work_form fky3j1ciorxegybim82k78tj15; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_work_form
    ADD CONSTRAINT fky3j1ciorxegybim82k78tj15 FOREIGN KEY (applicant_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

\unrestrict Pjd5HANeavvw92lpVhzRj5ZvUaj9m0r83NDt0A3FKKxL08yBzeNkbXzccRxDek7

