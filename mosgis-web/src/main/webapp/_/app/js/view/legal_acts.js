define ([], function () {

    var grid_name = 'legal_acts_grid'

    return function (data, view) {

        var it = data.item

        var postData = {data: {}}

        if (!$_USER.role.admin)
            postData.data.uuid_org = $_USER.uuid_org

        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarAdd: $_USER.has_nsi_20(7, 10),
                footer: 1,
                toolbarSearch: false
            },

            toolbar: {

                items: [
                ].filter (not_off),

            },

            textSearch: 'contains',

            searches: [
//                {field: 'level_', caption: 'Уровень', type: 'enum', options: {items: data.vc_legal_act_levels.items}},
                {field: 'docnumber', caption: 'Номер документа', type: 'text'},
            ].filter (not_off),

            columns: [

                {field: 'org.label', caption: 'Организация', size: 30},
                {field: 'id_ctr_status', caption: 'Статус', size: 20, voc: data.vc_gis_status},
                {field: 'level_', caption: 'Уровень', size: 20, voc: data.vc_legal_act_levels},
                {field: 'code_vc_nsi_324', caption: 'Вид документа', size: 30, voc: data.vc_nsi_324},
                {field: 'name', caption: 'Наименование документа', size: 100},
                {field: 'docnumber', caption: 'Номер документа', size: 30},
                {field: 'approvedate', caption: 'Дата принятия', size: 30, render: _dt},

            ].filter (not_off),

            postData: postData,

            url: '/mosgis/_rest/?type=legal_acts',

            onDblClick: function (e) {
                openTab ('/legal_act/' + e.recid)
            },

            onAdd: $_DO.create_legal_acts,

        })

    }

})