define ([], function () {

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['infrastructure_transportation_resources_grid']

        var t = g.toolbar

        t.disable ('deleteButton')
        t.disable ('editButton')

        if (g.getSelection ().length != 1) return

        var status = g.get (g.getSelection () [0]).sign

        if (!status) {
            t.enable ('deleteButton')
            t.enable ('editButton')
        }

    })}

    return function (data, view) {

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 
                 
            name: 'infrastructure_transportation_resources_grid',

            show: {
                toolbar: true,
                toolbarAdd: data.item._can.edit,
                toolbarInput: false,
                footer: true,
            },

            toolbar: {
                items: [
                    {type: 'button', id: 'editButton', caption: 'Изменить', onClick: $_DO.edit_infrastructure_transportation_resource, icon: 'w2ui-icon-pencil', off: !data.item._can.edit, disabled: true},
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_infrastructure_transportation_resource, icon: 'w2ui-icon-cross', off: !data.item._can.edit, disabled: true}
                ].filter (not_off)
            },

            columnGroups: [
                {span: 1, master: true},
                {span: 1, master: true},
                {span: 4, caption: 'Присоединенная нагрузка'}
            ],

            columns: [
                {field: 'code_vc_nsi_2', caption: 'Ресурс', size: 30, voc: data.vc_nsi_2_filtered},
                {field: 'volumelosses', caption: 'Объем потерь', size: 30},
                {field: 'totalload', caption: 'Общая', size: 30},
                {field: 'industrialload', caption: 'Промышленность', size: 30},
                {field: 'socialload', caption: 'Социальная сфера', size: 30},
                {field: 'populationload', caption: 'Население', size: 30}
            ],

            postData: {data: {uuid_oki: $_REQUEST.id}},
            url: '/_back/?type=infrastructure_transportation_resources',

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

            onAdd: $_DO.create_infrastructure_transportation_resource

        }).refresh ();

    }

})