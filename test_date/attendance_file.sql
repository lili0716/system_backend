--
-- PostgreSQL database dump
--

\restrict ojY5lumLptScVEV6XNZuWSifmmU24fWpc42atYZB7b2A8H5KQX0569yFcaSxf31

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
-- Name: attendance_file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attendance_file (
    id bigint NOT NULL,
    file_name character varying(255),
    file_path character varying(255),
    file_size bigint,
    file_type character varying(255),
    original_file_name character varying(255),
    parse_result character varying(255),
    parse_status integer,
    remark character varying(255),
    upload_time timestamp(6) without time zone,
    uploader_id bigint
);


--
-- Data for Name: attendance_file; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.attendance_file (id, file_name, file_path, file_size, file_type, original_file_name, parse_result, parse_status, remark, upload_time, uploader_id) FROM stdin;
352	b9098353-f948-426b-b9bf-dad1ed5a2df6_1.1~11.xls	D:\\atd_mini\\backend/uploads/attendance/b9098353-f948-426b-b9bf-dad1ed5a2df6_1.1~11.xls	1580544	application/vnd.ms-excel	1.1~11.xls	解析完成，成功: 2942 条，失败: 304 条	2	\N	\N	13224
\.


--
-- Name: attendance_file attendance_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_file
    ADD CONSTRAINT attendance_file_pkey PRIMARY KEY (id);


--
-- Name: attendance_file fknrr675yi9c173ymoctpjvfslp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_file
    ADD CONSTRAINT fknrr675yi9c173ymoctpjvfslp FOREIGN KEY (uploader_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

\unrestrict ojY5lumLptScVEV6XNZuWSifmmU24fWpc42atYZB7b2A8H5KQX0569yFcaSxf31

