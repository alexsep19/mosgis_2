define ([], function () {

    var grid_name = 'tarif_legal_acts_grid'

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui [grid_name]

        var t = g.toolbar

        t.disable ('deleteButton')

        if (g.getSelection ().length != 1) return

        t.enable ('deleteButton')

    })}

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
                toolbarAdd: true,
                toolbarDelete: false,
                footer: 1,
                toolbarSearch: false,
                toolbarInput: false,
                toolbarColumns: false
            },

            toolbar: {
                items: [
                    {type: 'button', id: 'deleteButton', caption: 'Исключить', onClick: $_DO.delete_tarif_legal_acts, icon: 'w2ui-icon-cross', disabled: true},
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

            postData: postData,

            url: '/mosgis/_rest/?type=tarif_legal_acts',

            onAdd: $_DO.create_tarif_legal_acts,

            onDelete: $_DO.delete_tarif_legal_acts,

            onSelect: recalcToolbar,

            onUnselect: recalcToolbar,
        })
    }

})