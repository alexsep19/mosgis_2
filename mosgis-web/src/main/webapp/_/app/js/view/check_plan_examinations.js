define ([], function () {

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['check_plan_examinations_grid']

        var t = g.toolbar

        t.disable ('deleteButton')

        if (g.getSelection ().length != 1) return

        var status = g.get (g.getSelection () [0]).sign

        if (!status) t.enable ('deleteButton')

    })}

    return function (data, view) {

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'check_plan_examinations_grid',

            show: {
                toolbar: true,
                toolbarAdd: true,
                toolbarInput: false,
                footer: true,
            },

            toolbar: {
                items: [
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_check_plan_examinations, icon: 'w2ui-icon-cross', disabled: true},
                ]
            },

            columns: [
                {field: 'numberinplan', caption: 'Номер', size: 5},
                {field: 'code_vc_nsi_71', caption: 'Форма проведения проверки', size: 10},
                {field: 'subject_label', caption: 'Субъект проверки', size: 15},
                {field: 'objective', caption: 'Цель проведения проверки', size: 30},
            ],

            postData: {data: {'plan_uuid': $_REQUEST.id}},

            url: '/mosgis/_rest/?type=planned_examinations',
            
            onAdd: $_DO.create_check_plan_examination,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

            onDblClick: function (e) {
                openTab ('/planned_examination/' + e.recid)
            }

        }).refresh ();

    }

})