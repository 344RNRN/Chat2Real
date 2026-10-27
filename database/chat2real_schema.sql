--
-- PostgreSQL database dump
--

-- Dumped from database version 16.3
-- Dumped by pg_dump version 16.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
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
-- Name: t005_participant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t005_participant (
    participant_id bigint NOT NULL
);


--
-- Name: participant_participant_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t005_participant ALTER COLUMN participant_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.participant_participant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t001_account; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t001_account (
    account_id bigint NOT NULL,
    login_id character varying(50) NOT NULL,
    password_hash character varying(255) NOT NULL,
    account_name character varying(100) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t001_account_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t001_account ALTER COLUMN account_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t001_account_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t002_meetup; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t002_meetup (
    meetup_id bigint NOT NULL,
    event_participant_id bigint NOT NULL,
    exchange_request_id bigint,
    meetup_result character varying(20) NOT NULL
);


--
-- Name: t002_meetup_meetup_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t002_meetup ALTER COLUMN meetup_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t002_meetup_meetup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t003_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t003_events (
    event_id bigint NOT NULL,
    account_id bigint NOT NULL,
    event_name character varying(150) NOT NULL,
    event_datetime timestamp without time zone NOT NULL,
    venue character varying(255) NOT NULL,
    event_status character varying(20) DEFAULT '準備中'::character varying NOT NULL,
    event_description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t003_events_event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t003_events ALTER COLUMN event_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t003_events_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t004_2_survey_answers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t004_2_survey_answers (
    answer_id bigint NOT NULL,
    event_participant_id bigint NOT NULL,
    question_id bigint NOT NULL,
    answer_text text NOT NULL
);


--
-- Name: t004_2_survey_answers_answer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t004_2_survey_answers ALTER COLUMN answer_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t004_2_survey_answers_answer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t004_survey_questions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t004_survey_questions (
    question_id bigint NOT NULL,
    event_id bigint NOT NULL,
    question_text character varying(500) NOT NULL,
    question_type character varying(20) DEFAULT 'text'::character varying NOT NULL,
    is_required boolean DEFAULT false NOT NULL,
    display_order integer DEFAULT 1 NOT NULL
);


--
-- Name: t004_survey_questions_question_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t004_survey_questions ALTER COLUMN question_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t004_survey_questions_question_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t006_events_participant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t006_events_participant (
    event_participant_id bigint NOT NULL,
    event_id bigint NOT NULL,
    participant_id bigint NOT NULL,
    profile_id bigint,
    exchange_status character varying(20) DEFAULT '交流したい'::character varying NOT NULL
);


--
-- Name: t007_events_participant_event_participant_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t006_events_participant ALTER COLUMN event_participant_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t007_events_participant_event_participant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t007_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t007_profiles (
    profile_id bigint NOT NULL,
    participant_id bigint NOT NULL,
    nickname character varying(20) NOT NULL,
    real_name character varying(30),
    age_group character varying(20) NOT NULL,
    gender character varying(10),
    self_introduction character varying(50),
    profile_image_url character varying(500)
);


--
-- Name: t007_profiles_profile_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t007_profiles ALTER COLUMN profile_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t007_profiles_profile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t008_interests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t008_interests (
    interest_id bigint NOT NULL,
    interest_name character varying(50) NOT NULL,
    is_active boolean DEFAULT true NOT NULL
);


--
-- Name: t008_interests_interest_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t008_interests ALTER COLUMN interest_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t008_interests_interest_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t009_participant_interests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t009_participant_interests (
    participant_interest_id bigint NOT NULL,
    event_id bigint NOT NULL,
    participant_id bigint NOT NULL,
    interest_id bigint NOT NULL
);


--
-- Name: t009_participant_interests_participant_interest_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t009_participant_interests ALTER COLUMN participant_interest_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t009_participant_interests_participant_interest_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t010_chats; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t010_chats (
    chat_id bigint NOT NULL,
    event_id bigint NOT NULL,
    chat_type character varying(20) DEFAULT '個人'::character varying NOT NULL,
    chat_status character varying(20) DEFAULT '利用中'::character varying NOT NULL
);


--
-- Name: t010_chats_chat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t010_chats ALTER COLUMN chat_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t010_chats_chat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t011_messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t011_messages (
    message_id bigint NOT NULL,
    chat_id bigint NOT NULL,
    sender_participant_id bigint NOT NULL,
    message_text text NOT NULL,
    sent_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t011_messages_message_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t011_messages ALTER COLUMN message_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t011_messages_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t012_exchange_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t012_exchange_requests (
    exchange_request_id bigint NOT NULL,
    chat_id bigint NOT NULL,
    requester_event_participant_id bigint NOT NULL,
    target_event_participant_id bigint NOT NULL,
    request_status character varying(20) DEFAULT '申請中'::character varying NOT NULL
);


--
-- Name: t012_exchange_requests_exchange_request_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t012_exchange_requests ALTER COLUMN exchange_request_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t012_exchange_requests_exchange_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t013_notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t013_notifications (
    notification_id bigint NOT NULL,
    event_id bigint NOT NULL,
    account_id bigint NOT NULL,
    title character varying(100) NOT NULL,
    notification_text text NOT NULL
);


--
-- Name: t013_notifications_notification_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.t013_notifications ALTER COLUMN notification_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.t013_notifications_notification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: t005_participant participant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t005_participant
    ADD CONSTRAINT participant_pkey PRIMARY KEY (participant_id);


--
-- Name: t001_account t001_account_login_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t001_account
    ADD CONSTRAINT t001_account_login_id_key UNIQUE (login_id);


--
-- Name: t001_account t001_account_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t001_account
    ADD CONSTRAINT t001_account_pkey PRIMARY KEY (account_id);


--
-- Name: t002_meetup t002_meetup_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t002_meetup
    ADD CONSTRAINT t002_meetup_pkey PRIMARY KEY (meetup_id);


--
-- Name: t003_events t003_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t003_events
    ADD CONSTRAINT t003_events_pkey PRIMARY KEY (event_id);


--
-- Name: t004_2_survey_answers t004_2_survey_answers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t004_2_survey_answers
    ADD CONSTRAINT t004_2_survey_answers_pkey PRIMARY KEY (answer_id);


--
-- Name: t004_survey_questions t004_survey_questions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t004_survey_questions
    ADD CONSTRAINT t004_survey_questions_pkey PRIMARY KEY (question_id);


--
-- Name: t006_events_participant t007_events_participant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t006_events_participant
    ADD CONSTRAINT t007_events_participant_pkey PRIMARY KEY (event_participant_id);


--
-- Name: t007_profiles t007_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t007_profiles
    ADD CONSTRAINT t007_profiles_pkey PRIMARY KEY (profile_id);


--
-- Name: t008_interests t008_interests_interest_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t008_interests
    ADD CONSTRAINT t008_interests_interest_name_key UNIQUE (interest_name);


--
-- Name: t008_interests t008_interests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t008_interests
    ADD CONSTRAINT t008_interests_pkey PRIMARY KEY (interest_id);


--
-- Name: t009_participant_interests t009_participant_interests_event_id_participant_id_interest_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t009_participant_interests
    ADD CONSTRAINT t009_participant_interests_event_id_participant_id_interest_key UNIQUE (event_id, participant_id, interest_id);


--
-- Name: t009_participant_interests t009_participant_interests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t009_participant_interests
    ADD CONSTRAINT t009_participant_interests_pkey PRIMARY KEY (participant_interest_id);


--
-- Name: t010_chats t010_chats_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t010_chats
    ADD CONSTRAINT t010_chats_pkey PRIMARY KEY (chat_id);


--
-- Name: t011_messages t011_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t011_messages
    ADD CONSTRAINT t011_messages_pkey PRIMARY KEY (message_id);


--
-- Name: t012_exchange_requests t012_exchange_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t012_exchange_requests
    ADD CONSTRAINT t012_exchange_requests_pkey PRIMARY KEY (exchange_request_id);


--
-- Name: t013_notifications t013_notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t013_notifications
    ADD CONSTRAINT t013_notifications_pkey PRIMARY KEY (notification_id);


--
-- Name: t002_meetup t002_meetup_event_participant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t002_meetup
    ADD CONSTRAINT t002_meetup_event_participant_id_fkey FOREIGN KEY (event_participant_id) REFERENCES public.t006_events_participant(event_participant_id);


--
-- Name: t002_meetup t002_meetup_exchange_request_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t002_meetup
    ADD CONSTRAINT t002_meetup_exchange_request_id_fkey FOREIGN KEY (exchange_request_id) REFERENCES public.t012_exchange_requests(exchange_request_id);


--
-- Name: t003_events t003_events_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t003_events
    ADD CONSTRAINT t003_events_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.t001_account(account_id);


--
-- Name: t004_2_survey_answers t004_2_survey_answers_event_participant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t004_2_survey_answers
    ADD CONSTRAINT t004_2_survey_answers_event_participant_id_fkey FOREIGN KEY (event_participant_id) REFERENCES public.t006_events_participant(event_participant_id);


--
-- Name: t004_2_survey_answers t004_2_survey_answers_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t004_2_survey_answers
    ADD CONSTRAINT t004_2_survey_answers_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.t004_survey_questions(question_id);


--
-- Name: t004_survey_questions t004_survey_questions_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t004_survey_questions
    ADD CONSTRAINT t004_survey_questions_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.t003_events(event_id);


--
-- Name: t006_events_participant t007_events_participant_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t006_events_participant
    ADD CONSTRAINT t007_events_participant_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.t003_events(event_id);


--
-- Name: t006_events_participant t007_events_participant_participant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t006_events_participant
    ADD CONSTRAINT t007_events_participant_participant_id_fkey FOREIGN KEY (participant_id) REFERENCES public.t005_participant(participant_id);


--
-- Name: t006_events_participant t007_events_participant_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t006_events_participant
    ADD CONSTRAINT t007_events_participant_profile_id_fkey FOREIGN KEY (profile_id) REFERENCES public.t007_profiles(profile_id);


--
-- Name: t007_profiles t007_profiles_participant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t007_profiles
    ADD CONSTRAINT t007_profiles_participant_id_fkey FOREIGN KEY (participant_id) REFERENCES public.t005_participant(participant_id);


--
-- Name: t009_participant_interests t009_participant_interests_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t009_participant_interests
    ADD CONSTRAINT t009_participant_interests_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.t003_events(event_id);


--
-- Name: t009_participant_interests t009_participant_interests_interest_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t009_participant_interests
    ADD CONSTRAINT t009_participant_interests_interest_id_fkey FOREIGN KEY (interest_id) REFERENCES public.t008_interests(interest_id);


--
-- Name: t009_participant_interests t009_participant_interests_participant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t009_participant_interests
    ADD CONSTRAINT t009_participant_interests_participant_id_fkey FOREIGN KEY (participant_id) REFERENCES public.t005_participant(participant_id);


--
-- Name: t010_chats t010_chats_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t010_chats
    ADD CONSTRAINT t010_chats_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.t003_events(event_id);


--
-- Name: t011_messages t011_messages_chat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t011_messages
    ADD CONSTRAINT t011_messages_chat_id_fkey FOREIGN KEY (chat_id) REFERENCES public.t010_chats(chat_id);


--
-- Name: t011_messages t011_messages_sender_participant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t011_messages
    ADD CONSTRAINT t011_messages_sender_participant_id_fkey FOREIGN KEY (sender_participant_id) REFERENCES public.t005_participant(participant_id);


--
-- Name: t012_exchange_requests t012_exchange_requests_chat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t012_exchange_requests
    ADD CONSTRAINT t012_exchange_requests_chat_id_fkey FOREIGN KEY (chat_id) REFERENCES public.t010_chats(chat_id);


--
-- Name: t012_exchange_requests t012_exchange_requests_requester_event_participant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t012_exchange_requests
    ADD CONSTRAINT t012_exchange_requests_requester_event_participant_id_fkey FOREIGN KEY (requester_event_participant_id) REFERENCES public.t006_events_participant(event_participant_id);


--
-- Name: t012_exchange_requests t012_exchange_requests_target_event_participant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t012_exchange_requests
    ADD CONSTRAINT t012_exchange_requests_target_event_participant_id_fkey FOREIGN KEY (target_event_participant_id) REFERENCES public.t006_events_participant(event_participant_id);


--
-- Name: t013_notifications t013_notifications_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t013_notifications
    ADD CONSTRAINT t013_notifications_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.t001_account(account_id);


--
-- Name: t013_notifications t013_notifications_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t013_notifications
    ADD CONSTRAINT t013_notifications_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.t003_events(event_id);


--
-- PostgreSQL database dump complete
--

