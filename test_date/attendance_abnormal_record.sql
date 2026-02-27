--
-- PostgreSQL database dump
--

\restrict WV1Ud57TE7K5fJrK42SvtKSpx4ehK3M0g1oz3alMq29z4irZSwoaVc8OyxZMYb6

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
-- Name: attendance_abnormal_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attendance_abnormal_record (
    id bigint NOT NULL,
    abnormal_type integer,
    corrected_by_form_id bigint,
    create_time timestamp(6) without time zone,
    diff_minutes integer,
    expected_time timestamp(6) without time zone,
    is_corrected boolean,
    original_time timestamp(6) without time zone,
    record_date timestamp(6) without time zone,
    attendance_record_id bigint,
    user_id bigint
);


--
-- Data for Name: attendance_abnormal_record; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.attendance_abnormal_record (id, abnormal_type, corrected_by_form_id, create_time, diff_minutes, expected_time, is_corrected, original_time, record_date, attendance_record_id, user_id) FROM stdin;
\.


--
-- Name: attendance_abnormal_record attendance_abnormal_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_abnormal_record
    ADD CONSTRAINT attendance_abnormal_record_pkey PRIMARY KEY (id);


--
-- Name: attendance_abnormal_record fk69bh75mnpjss990cdj51l1t7c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_abnormal_record
    ADD CONSTRAINT fk69bh75mnpjss990cdj51l1t7c FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: attendance_abnormal_record fk7o0ka769wa54aid2h6yemgbxt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_abnormal_record
    ADD CONSTRAINT fk7o0ka769wa54aid2h6yemgbxt FOREIGN KEY (attendance_record_id) REFERENCES public.attendance_record(id);


--
-- PostgreSQL database dump complete
--

\unrestrict WV1Ud57TE7K5fJrK42SvtKSpx4ehK3M0g1oz3alMq29z4irZSwoaVc8OyxZMYb6

