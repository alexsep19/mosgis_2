define ([], function () {

    var grid_name = 'tarif_coeffs_grid'

    return function (data, view) {

        var it = data.item

        var postData = {data: {uuid_tf: $_REQUEST.id}}

        if (!$_USER.role.admin)
            postData.data.uuid_org = $_USER.uuid_org

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarAdd: data._can.create,
                toolbarDelete: data._can.delete,
                toolbarEdit: data._can.edit,
                footer: 1,
                toolbarSearch: false,
                toolbarInput: false,
                toolbarColumns: false
            },

            textSearch: 'contains',

            searches: [
            ].filter (not_off),

            columns: [

                {field: 'coefficientvalue', caption: 'Значение коэффициента', size: 20},
                {field: 'price', caption: 'Величина', size: 20},
                {field: 'coefficientdescription', caption: 'Описание коэффициента', size: 100},

            ].filter (not_off),

            postData: postData,

            url: '/mosgis/_rest/?type=tarif_coeffs',
            onRefresh: function() {
                $('div.w2ui-grid-toolbar table td:last').html('<span style="padding: 10px">В случае если при расчете платы за наем жилого помещения к указанной ставке применяется несколько коэффициентов, коэффициенты перемножаются</span>')
            },

            onDblClick: data._can.edit? $_DO.edit_tarif_coeffs : null,

            onAdd: $_DO.create_tarif_coeffs,

            onEdit: $_DO.edit_tarif_coeffs,

            onDelete: $_DO.delete_tarif_coeffs
        })
    }

})