define ([], function () {

    var grid_name = 'citizen_compensation_category_legal_acts_grid'

    return function (data, view) {

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarAdd: false,
                toolbarDelete: false,
                footer: 1,
                toolbarSearch: false,
                toolbarInput: false,
                toolbarColumns: false
            },

            toolbar: {
                items: [
                ].filter(not_off),
            },

            textSearch: 'contains',

            searches: [
            ].filter (not_off),

            columns: [

                {field: 'docnumber', caption: 'Номер', size: 30},
                {field: 'name', caption: 'Наименование', size: 100},
                {field: 'approvedate', caption: 'Дата вступления в силу', size: 30, render: _dt},
                {field: 'level_', caption: 'Уровень', size: 20, voc: data.vc_legal_act_levels},
                {field: 'id_ctr_status', caption: 'Статус', size: 20, voc: data.vc_gis_status},
                {field: 'code_vc_nsi_324', caption: 'Вид', size: 30, voc: data.vc_nsi_324},

            ].filter (not_off),

            postData: {data: {uuid_org: $_USER.uuid_org}},

            url: '/_back/?type=citizen_compensation_categories&part=legal_acts&id=' + $_REQUEST.id,

            onDblClick: function (e) { openTab ('/legal_act/' + e.recid) },
        })
    }

})