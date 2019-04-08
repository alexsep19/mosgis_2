define ([], function () {

    function perms () {

        var signed = clone ($('body').data ('data')).item.id_ctr_status == 41

        return $_USER.role.nsi_20_4 && !signed

    }

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['check_plan_examinations_grid']

        var t = g.toolbar

        t.disable ('deleteButton')

        if (g.getSelection ().length != 1) return

        var status = g.get (g.getSelection () [0]).id_ctr_status

        if (status != 41) t.enable ('deleteButton')

    })}

    return function (data, view) {

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'check_plan_examinations_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            toolbar: {
                items: [
                    {type: 'button', id: 'createButton', caption: 'Добавить', onClick: $_DO.create_check_plan_examination, icon: 'w2ui-icon-plus', off: !perms ()},
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_check_plan_examination, icon: 'w2ui-icon-cross', disabled: true, off: !perms ()},
                ].filter (not_off)
            },

            searches: [
                {field: 'code_vc_nsi_71', caption: 'Форма проведения проверки', type: 'enum', options: {items: data.vc_nsi_71.items}},
                {field: 'code_vc_nsi_65', caption: 'Вид осуществления контрольной деятельности', type: 'enum', options: {items: data.vc_nsi_65.items}}
            ],

            columns: [
                {field: 'numberinplan', caption: 'Номер', size: 5},
                {field: 'code_vc_nsi_71', caption: 'Форма проведения проверки', size: 10, voc: data.vc_nsi_71},
                {field: 'subject_label', caption: 'Субъект проверки', size: 15},
                {field: 'objective', caption: 'Цель проведения проверки', size: 30},
            ],

            postData: {data: {'plan_uuid': $_REQUEST.id}},

            url: '/_back/?type=planned_examinations',
            
            onAdd: $_DO.create_check_plan_examination,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

            onDblClick: function (e) {
                openTab ('/planned_examination/' + e.recid)
            }

        }).refresh ();

    }

})