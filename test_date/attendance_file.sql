--
-- PostgreSQL database dump
--

\restrict 8Y5kP2X2XKmgyzHYQnu3hbf2Ugfr8Al0vylLOJUrdfaMORLs6ruroddmByV1Vfr

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
52	780fec2e-ed55-44c6-9515-e502f2039e1a_1.1~11.xls	D:\\atd_mini\\backend\\uploads\\attendance\\780fec2e-ed55-44c6-9515-e502f2039e1a_1.1~11.xls	1580544	application/vnd.ms-excel	1.1~11.xls	解析成功，共生成 2942 条考勤记录	2	\N	\N	13224
102	722a623c-0858-4cb7-8de6-353f2943d8af_1.12~18.xls	D:\\atd_mini\\backend\\uploads\\attendance\\722a623c-0858-4cb7-8de6-353f2943d8af_1.12~18.xls	1301504	application/vnd.ms-excel	1.12~18.xls	解析成功，共生成 2196 条考勤记录	2	\N	\N	13224
152	cc6b3af8-55eb-4a85-899e-c5f03f980cd5_1.12~18.xls	D:\\atd_mini\\backend\\uploads\\attendance\\cc6b3af8-55eb-4a85-899e-c5f03f980cd5_1.12~18.xls	1301504	application/vnd.ms-excel	1.12~18.xls	解析成功，共生成 2196 条考勤记录	2	\N	\N	13224
153	96e21966-f03a-4665-8320-4c4148683728_1.12~18.xls	D:\\atd_mini\\backend\\uploads\\attendance\\96e21966-f03a-4665-8320-4c4148683728_1.12~18.xls	1301504	application/vnd.ms-excel	1.12~18.xls	解析成功，共生成 2196 条考勤记录	2	\N	\N	13224
202	f3506f7a-634b-49e3-b05b-43a7bf3e33f3_1.12~18.xls	D:\\atd_mini\\backend\\uploads\\attendance\\f3506f7a-634b-49e3-b05b-43a7bf3e33f3_1.12~18.xls	1301504	application/vnd.ms-excel	1.12~18.xls	解析成功，共生成 2196 条考勤记录	2	\N	\N	13224
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

\unrestrict 8Y5kP2X2XKmgyzHYQnu3hbf2Ugfr8Al0vylLOJUrdfaMORLs6ruroddmByV1Vfr

