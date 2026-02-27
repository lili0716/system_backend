--
-- PostgreSQL database dump
--

\restrict pLS2aGervLE92ZjawY74PiAMPmB1nY8RxDEQRRDIGGp3erZtdBfYlmS07OyFhC1

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
-- Name: attendance_rule; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attendance_rule (
    id bigint NOT NULL,
    create_time timestamp(6) without time zone,
    description character varying(255),
    early_leave_threshold integer,
    enabled boolean,
    flexible_work_in_end timestamp(6) without time zone,
    flexible_work_in_start timestamp(6) without time zone,
    flexible_work_out_end timestamp(6) without time zone,
    flexible_work_out_start timestamp(6) without time zone,
    late_threshold integer,
    rule_name character varying(255),
    standard_work_hours double precision,
    update_time timestamp(6) without time zone,
    work_in_time timestamp(6) without time zone,
    work_out_time timestamp(6) without time zone,
    department_id bigint,
    flexible_time_range integer,
    single_week_off boolean,
    is_default boolean
);


--
-- Data for Name: attendance_rule; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.attendance_rule (id, create_time, description, early_leave_threshold, enabled, flexible_work_in_end, flexible_work_in_start, flexible_work_out_end, flexible_work_out_start, late_threshold, rule_name, standard_work_hours, update_time, work_in_time, work_out_time, department_id, flexible_time_range, single_week_off, is_default) FROM stdin;
2	\N	系统默认考勤规则	0	t	\N	\N	\N	\N	0	默认考勤规则	8	\N	1970-01-01 16:30:00	1970-01-02 01:00:00	\N	5	\N	t
\.


--
-- Name: attendance_rule attendance_rule_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_rule
    ADD CONSTRAINT attendance_rule_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

\unrestrict pLS2aGervLE92ZjawY74PiAMPmB1nY8RxDEQRRDIGGp3erZtdBfYlmS07OyFhC1

