define ([], function () {

    var grid_name = 'citizen_compensation_to_categories_grid'

    return function (data, view) {

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarAdd: data._can.create,
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

            columnGroups : [
                {master: true},
                {span: 2, caption: 'Период предоставления'},
                {master: true},
            ],            

            columns: [
                {field: 'ct.label', caption: 'Категория', size: 100},
                {field: 'periodfrom', caption: 'Дата начала', size: 20, render: _dt},
                {field: 'periodto', caption: 'Дата окончания', size: 20, render: _dt},
                {field: 'svc_types', caption: 'Расходы, подлежащие компенсации', size: 50, render: (i) => {
                    i.vc_svc_types = i.vc_svc_types || []
                    return i.vc_svc_types.map(id => data.vc_svc_types [id]).join('; ')
                }},
            ].filter (not_off),

            postData: {data: {uuid_cit_comp_cat: $_REQUEST.id}},

            url: '/_back/?type=citizen_compensation_to_categories',

            onDblClick: $_DO.edit_citizen_compensation_to_categories,

            onAdd: $_DO.create_citizen_compensation_to_categories,
        })

    }

})