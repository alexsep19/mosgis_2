define ([], function () {

    return function (data, view) {

        data.label = 'Информация о состоянии расчетов'

        $('title').text(data.label)

        fill(view, data, $('#body'))

        $('#container').w2regrid({

            name: 'settlement_docs_grid',

            show: {
                toolbarSearch: false,
                toolbarInput: false,
                toolbarReload: false,
                toolbar: true,
                toolbarAdd: true,
                toolbarDelete: false,
                footer: true,
            },

            searches: [
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}}

            ].filter (not_off),

            columns: [
                {field: 'id_type', caption: 'Тип', voc: data.vc_st_doc_types, size: 50},
                {field: 'org.label', caption: 'Поставщик информации', size: 50},
                {field: 'sr_ctr.label', caption: 'Договор', size: 50},
                {field: 'org_executor.label', caption: 'Ресурсоснабжающая организация', size: 50},
                {field: 'org_customer.label', caption: 'Исполнитель коммунальной услуги', size: 50}
            ].filter (not_off),

            url: '/_back/?type=settlement_docs',

            postData: {
                data: {
                    uuid_org: $_USER.uuid_org
                }
            },

            onAdd:      $_DO.create_settlement_docs,

            onDblClick: function (e) {
                openTab ('/settlement_doc/' + e.recid)
            },

        }).refresh ();

    }

})