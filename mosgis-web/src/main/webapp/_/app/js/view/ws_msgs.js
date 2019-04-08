define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['integration_layout'].el ('main')).w2regrid ({ 

            name: 'ws_msgs_grid',

            show: {
                toolbar: true,
                toolbarSearch: true,
                toolbarInput: false,
                footer: true,
            },
            
            searches: [            
                {field: 'uuid_sender', caption: 'Внешняя ИС',  type: 'enum', options: {items: data.tb_senders.items}},
                {field: 'operation', caption: 'Метод',  type: 'text'},
                {field: 'request_time', caption: 'Дата запроса',  type: 'date'},
                {field: 'id_status', caption: 'Статус обработки',  type: 'enum', options: {items: data.vc_async_request_states.items}},
                {field: 'has_error', caption: 'Наличие ошибки', type: 'enum', options: {items: [
                    {id: "0", text: "Нет"},
                    {id: "1", text: "Да"},
                ]}}
//                {field: 'id_ctr_status', caption: 'Статус договора', type: 'enum', options: {items: data.vc_gis_status.items.filter (function (i) {
//                    switch (i.id) {
//                        case 50:
//                        case 60:
//                        case 80:
//                            return false;
//                        default:
//                            return true;
//                    }
//                })}}, 
//                {field: 'uuid_org', caption: 'Исполнитель', type: 'enum', options: {items: data.vc_orgs.items}, off: $_USER.role.nsi_20_1},
//                {field: 'id_customer_type', caption: 'Тип заказчика', type: 'enum', options: {items: data.vc_gis_customer_type.items}, off: !($_USER.role.admin || $_USER.role.nsi_20_1)},
//                {field: 'uuid_org_customer', caption: 'Заказчик', type: 'enum', options: {items: data.customers.items}, off: !($_USER.role.admin || $_USER.role.nsi_20_1)},
//                {field: 'contractbase', caption: 'Основание заключения', type: 'enum', options: {items: data.vc_nsi_58.items}},
//                {field: 'effectivedate', caption: 'Дата вступления в силу',  type: 'date'},
//                {field: 'plandatecomptetion', caption: 'Дата окончания',  type: 'date'},
//                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
//                    {id: "0", text: "Актуальные"},
//                    {id: "1", text: "Удалённые"},
//                ]}},
            ].filter (not_off),

            columns: [                
                {field: 'sender_name', caption: 'Внешняя ИС',    size: 10},
                {field: 'org_name', caption: 'Организация',    size: 20},
                {field: 'operation', caption: 'Метод',    size: 30},
                {field: 'uuid_message', caption: 'Входящий идентификатор пакета',    size: 30},
                {field: 'uuid', caption: 'Исходящий идентификатор пакета',    size: 30},
                {field: 'request_time', caption: 'Дата запроса',  size: 20, render: _ts, attr: 'data-ref=1'},
                {field: 'response_time', caption: 'Дата формирования ответа',  size: 20, render: _ts, attr: 'data-ref=1'},
                {field: 'id_status', caption: 'Статус',  size: 10, voc: data.vc_async_request_states},
                {field: 'has_error', caption: 'Наличие<br>ошибки', size: 10, voc: {1: 'да', 0: ''}},
            ],

            url: '/_back/?type=ws_msgs',
            
            onClick: function (e) {
            
                var c = this.columns [e.column]
                var r = this.get (e.recid)
                
                switch (c.field) {
                    case 'request_time': return openTab ('/ws_msg_rq/' + r.uuid)
                    case 'response_time': if (r.response) return openTab ('/ws_msg_rp/' + r.uuid)
                }
            
            },

        }).refresh ();

    }

})