define ([], function () {

    var grid_name = 'citizen_compensation_payments_grid'

    return function (data, view) {

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarAdd: data.item._can.update,
                toolbarEdit: data.item._can.update,
                toolbarDelete: data.item._can.update,
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
                {field: 'paymenttype', caption: 'Тип выплаты', size: 100, voc: data.vc_cit_comp_pay_types},
                {field: 'paymentdate', caption: 'Дата выплаты', size: 20, render: _dt},
                {field: 'paymentsum', caption: 'Сумма выплаты', size: 20, render: 'money:2'},
            ].filter (not_off),

            postData: {data: {uuid_cit_comp: $_REQUEST.id}},

            url: '/_back/?type=citizen_compensation_payments',


            onDblClick: data.item._can.update? $_DO.edit_citizen_compensation_payments : null,

            onEdit: $_DO.edit_citizen_compensation_payments,

            onAdd: $_DO.create_citizen_compensation_payments,

            onDelete: $_DO.delete_citizen_compensation_payments,
        })

    }

})