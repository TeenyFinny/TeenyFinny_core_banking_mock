create table if not exists core_investment_account
(
    cano         varchar(20) not null
        primary key,
    created_at   datetime(6) not null,
    dnca_tot_amt bigint      not null,
    user_id      bigint      not null
);

create table if not exists core_portfolio
(
    portfolio_id  bigint auto_increment
        primary key,
    created_at    datetime(6) not null,
    hldg_qty      bigint      not null,
    pdno          varchar(12) not null,
    prdt_name     varchar(60) not null,
    pchs_avg_pric bigint      not null,
    user_id       bigint      not null,
    cano          varchar(20) not null,
    constraint FKa89uvrrhcudcyshrh3qdxf1dd
        foreign key (cano) references core_investment_account (cano)
);

create table if not exists core_portfolio_monthly
(
    id                bigint auto_increment
        primary key,
    cano              varchar(255) not null,
    created_at        datetime(6)  not null,
    current_price     bigint       not null,
    evaluation_amount bigint       not null,
    hldg_qty          bigint       not null,
    month             int          not null,
    pdno              varchar(255) not null,
    prdt_name         varchar(255) not null,
    profit_amount     bigint       not null,
    profit_rate       double       not null,
    pchs_avg_pric     bigint       not null,
    user_id           bigint       not null,
    weight            double       not null,
    year              int          not null
);

create table if not exists core_portfolio_monthly_summary
(
    id                      bigint auto_increment
        primary key,
    cano                    varchar(255) not null,
    created_at              datetime(6)  null,
    deposit_amount          bigint       not null,
    etc_weight              double       null,
    month                   int          not null,
    top1_name               varchar(255) null,
    top1_weight             double       null,
    top2_name               varchar(255) null,
    top2_weight             double       null,
    top3_name               varchar(255) null,
    top3_weight             double       null,
    total_evaluation_amount bigint       not null,
    total_profit_amount     bigint       not null,
    total_profit_rate       double       not null,
    user_id                 bigint       not null,
    year                    int          not null
);

create table if not exists core_trade_orders
(
    order_id        bigint auto_increment
        primary key,
    created_at      datetime(6)                                                                            not null,
    excg_id_dvsn_cd varchar(3)                                                                             not null,
    gt_uid          varchar(32)                                                                            null,
    ord_tmd         datetime(6)                                                                            not null,
    ord_unpr        bigint                                                                                 not null,
    pdno            varchar(12)                                                                            not null,
    prdt_name       varchar(60)                                                                            not null,
    ord_qty         int                                                                                    not null,
    status          enum ('CANCELLED', 'EXECUTED', 'REQUESTED')                                            not null,
    tr_id           enum ('FHKST010100', 'TTC8348R', 'TTTCO011U', 'TTTCO8408R', 'TTTCO898R', 'TTTTC0012U') not null,
    user_id         bigint                                                                                 not null,
    cano            varchar(20)                                                                            not null,
    constraint FK6i1qvftku77eqliesyht3w0tb
        foreign key (cano) references core_investment_account (cano)
);

create table if not exists core_users
(
    user_id         bigint auto_increment
        primary key,
    created_at      datetime(6) not null,
    birth_date      date        not null,
    channel_user_id bigint      not null,
    name            varchar(50) not null,
    phone_number    varchar(20) not null
);

create table if not exists core_account
(
    account_id    bigint auto_increment
        primary key,
    created_at    datetime(6)                                         not null,
    updated_at    datetime(6)                                         not null,
    balance       decimal(18, 2)                                      not null,
    expired_at    date                                                null,
    interest_rate decimal(5, 3)                                       not null,
    number        varchar(20)                                         not null,
    product_name  varchar(100)                                        not null,
    status        enum ('ACTIVE', 'CLOSED', 'SUSPENDED')              not null,
    type          enum ('ALLOWANCE', 'DEPOSIT', 'GOAL', 'INVESTMENT') not null,
    user_id       bigint                                              not null,
    constraint UK1ovbnvthgnidrly3n1tpqbjo2
        unique (number),
    constraint FK4yyiw0m847nqvawwbjk26d8ty
        foreign key (user_id) references core_users (user_id)
);

create table if not exists core_auto_transfer
(
    auto_transfer_id  bigint auto_increment
        primary key,
    created_at        datetime(6)                            not null,
    updated_at        datetime(6)                            not null,
    amount            decimal(15, 2)                         not null,
    memo              varchar(50)                            null,
    next_transfer_day date                                   null,
    status            enum ('FAIL', 'PROCESSING', 'SUCCESS') not null,
    transfer_day      int                                    not null,
    from_account_id   bigint                                 not null,
    to_account_id     bigint                                 not null,
    user_id           bigint                                 not null,
    constraint FKbdfscu3d76ka3baibe1v6d168
        foreign key (to_account_id) references core_account (account_id),
    constraint FKooan3ryd3te1xuc388m3iymj3
        foreign key (from_account_id) references core_account (account_id),
    constraint FKqiuvg9msf6x9asxv04gdpc9tc
        foreign key (user_id) references core_users (user_id)
);

create table if not exists core_transaction
(
    transaction_id   bigint auto_increment
        primary key,
    created_at       datetime(6)                         not null,
    updated_at       datetime(6)                         not null,
    amount           decimal(15, 3)                      not null,
    balance_after    decimal(15, 3)                      not null,
    category         tinyint                             not null,
    code             varchar(10)                         not null,
    merchant_name    varchar(50)                         not null,
    status           enum ('FAIL', 'PENDING', 'SUCCESS') not null,
    transaction_date datetime(6)                         not null,
    type             tinyint                             null,
    account_id       bigint                              not null,
    user_id          bigint                              not null,
    constraint FK3t09heg421w4yfhbt1pp9sry0
        foreign key (user_id) references core_users (user_id),
    constraint FKpdggopp8yw4pj8p9d9ppwadwf
        foreign key (account_id) references core_account (account_id)
);

create table if not exists core_user_relationship
(
    relationship_id bigint auto_increment
        primary key,
    created_at      datetime(6) not null,
    child_id        bigint      not null,
    parent_id       bigint      not null,
    constraint FKen2bne2spcw8dd2qoix828f8h
        foreign key (parent_id) references core_users (user_id),
    constraint FKtf928gb8mspdhcu5toxuso3sq
        foreign key (child_id) references core_users (user_id)
);

create table if not exists sample_entity
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    price      varchar(64) not null
);


