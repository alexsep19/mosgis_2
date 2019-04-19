define ([], function () {

    var grid_name = 'citizen_compensation_calcs_grid'

    return function (data, view) {

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({

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
                {field: 'periodfrom', caption: 'Дата начала расчета', size: 20, render: _dt},
                {field: 'periodto', caption: 'Дата окончания расчета', size: 20, render: _dt},
                {field: 'calculationdate', caption: 'Дата расчета', size: 20, render: _dt},
                {field: 'compensationsum', caption: 'Размер компенсационной выплаты', size: 20, render: 'money:2'},
            ].filter (not_off),

            postData: {data: {uuid_cit_comp: $_REQUEST.id}},

            url: '/_back/?type=citizen_compensation_calcs',


            onDblClick: data.item._can.update? $_DO.edit_citizen_compensation_calcs : null,

            onEdit: $_DO.edit_citizen_compensation_calcs,

            onAdd: $_DO.create_citizen_compensation_calcs,

            onDelete: $_DO.delete_citizen_compensation_calcs,
        })

    }

})