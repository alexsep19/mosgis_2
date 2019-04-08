define ([], function () {

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['infrastructure_net_pieces_grid']

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
                 
            name: 'infrastructure_net_pieces_grid',

            show: {
                toolbar: true,
                toolbarAdd: data.item._can.edit,
                toolbarInput: false,
                footer: true,
            },

            toolbar: {
                items: [
                    {type: 'button', id: 'editButton', caption: 'Изменить', onClick: $_DO.edit_infrastructure_net_piece, icon: 'w2ui-icon-pencil', off: !data.item._can.edit, disabled: true},
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_infrastructure_net_piece, icon: 'w2ui-icon-cross', off: !data.item._can.edit, disabled: true}
                ].filter (not_off)
            },

            columns: [
                {field: 'name', caption: 'Наименование участка', size: 30},
                {field: 'diameter', caption: 'Диаметр (мм)', size: 10},
                {field: 'length', caption: 'Протяженность (км)', size: 20},
                {field: 'needreplaced', caption: 'Нуждается в замене (км)', size: 20},
                {field: 'wearout', caption: 'Износ (%)', size: 10},
            ],

            postData: {data: {uuid_oki: $_REQUEST.id}},
            url: '/_back/?type=infrastructure_net_pieces',

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

            onAdd: $_DO.create_infrastructure_net_piece

        }).refresh ();

    }

})