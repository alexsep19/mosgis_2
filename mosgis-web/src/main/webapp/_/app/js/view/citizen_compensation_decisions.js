define ([], function () {

    var grid_name = 'citizen_compensation_decisions_grid'

    return function (data, view) {

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarAdd: data.item._can.update,
                toolbarEdit: data.item._can.update,
                toolbarDelete: data.item._can.delete,
                footer: 1,
                toolbarSearch: false,
                toolbarInput: false,
                toolbarColumns: false,
            },

            toolbar: {

                items: [
                ].filter (not_off),
            },

            textSearch: 'contains',

            searches: [
            ].filter (not_off),

            columns: [
                {field: 'number_', caption: 'Номер решения', size: 100},
                {field: 'decisiondate', caption: 'Дата решения', size: 20, render: _dt},
                {field: 'code_vc_nsi_301', caption: 'Тип решения', size: 20, voc: data.vc_nsi_301},
                {field: 'code_vc_nsi_302', caption: 'Основание решения', size: 20, voc: data.vc_nsi_302},
                {field: 'eventdate', caption: 'Дата события', size: 20, render: _dt},
            ].filter (not_off),

            postData: {data: {uuid_cit_comp: $_REQUEST.id}},

            url: '/_back/?type=citizen_compensation_decisions',


            onDblClick: data.item._can.update? $_DO.edit_citizen_compensation_decisions : null,

            onEdit: $_DO.edit_citizen_compensation_decisions,

            onAdd: $_DO.create_citizen_compensation_decisions,

            onDelete: $_DO.delete_citizen_compensation_decisions,
        })

    }

})