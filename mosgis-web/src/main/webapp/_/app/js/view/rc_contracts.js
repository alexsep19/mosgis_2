define ([], function () {

    var b = ['delete', 'undelete']

    var postData = {}

    if ($_USER.role.nsi_20_36)
        postData.uuid_org = $_USER.uuid_org
    if ($_USER.has_nsi_20(1, 2) ||  $_USER.is_building_society())
        postData.uuid_org_customer = $_USER.uuid_org
    if ($_USER.role.nsi_20_8)
        postData.is_oms = 1

    return function (data, view) {

        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({

            name: 'rc_contracts_grid',

            show: {
                toolbarSearch: true,
                toolbar: true,
                footer: true,
                toolbarAdd: data._can.add
            },

            searches: [
                {field: 'id_ctr_status', caption: 'Статус договора', type: 'enum'
                    , options: {items: data.vc_gis_status.items.filter (function (i) {
                    switch (i.id) {
                        case 10:
                        case 40:
                        case 100:
                        case 110:
                            return true;
                        default:
                            return false;
                    }
                })}},
                {field: 'customer_label_uc', caption: 'Заказчик', type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}}

            ].filter (not_off),

            columns: [
                {field: 'contractnumber', caption: 'Номер', size: 20},
                {field: 'signingdate', caption: 'Дата заключения', size: 18, render: _dt},
                {field: 'effectivedate', caption: 'Дата вступления в силу', size: 25, render: _dt},
                {field: 'id_ctr_status', caption: 'Статус', size: 10, voc: data.vc_gis_status},
                {field: 'org_label', caption: 'Расчетный центр', size: 100},
                {field: 'customer_label', caption: 'Заказчик', size: 100},
                {field: 'completiondate', caption: 'Дата окончания', size: 18, render: _dt},
            ].filter (not_off),

            postData: {data: postData},

            url: '/mosgis/_rest/?type=rc_contracts',

            onAdd:      $_DO.create_rc_contracts,

            onDblClick: function (e) {
                openTab ('/rc_contract/' + e.recid)
            },

        }).refresh ();

    }

})