define ([], function () {

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['vc_oh_wk_types_grid']
        var t = g.toolbar

        t.disable ('edit')
        t.disable ('delete')
        
        if (g.getSelection ().length != 1) return

        if (g.get (g.getSelection () [0]).id_owt_status == 14) {
            t.enable ('edit')
            t.enable ('delete')
        }

    })}

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({ 

            name: 'vc_oh_wk_types_grid',

            toolbar: {
            
                items: [
                
                    {
                        type: 'button', 
                        id: 'import', 
                        caption: 'Импорт справочника из ГИС ЖКХ', 
                        icon: 'w2ui-icon-plus', 
                        onClick: $_DO.import_vc_oh_wk_types,
                        off: !$_USER.role.nsi_20_7
                    },

                    {
                        type: 'button',
                        id: 'edit',
                        caption: 'Изменить',
                        icon: 'w2ui-icon-pencil',
                        onClick: $_DO.edit_vc_oh_wk_types,
                        off: !$_USER.role.nsi_20_7,
                        disabled: true
                    },

                    {
                        type: 'button',
                        id: 'delete',
                        caption: 'Удалить',
                        icon: 'w2ui-icon-cross',
                        onClick: $_DO.delete_vc_oh_wk_types,
                        off: !$_USER.role.nsi_20_7,
                        disabled: true
                    },
                    
                ].filter (not_off),
                
            },

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: $_USER.role.nsi_20_7,
                footer: true,
            },

            columns: [
                {field: 'code', caption: 'Код', size: 10},
                {field: 'servicename', caption: 'Наименование', size: 10},
                {field: 'code_vc_nsi_218', caption: 'Группа работ', size: 10, voc: data.vc_nsi_218},
                {field: 'id_owt_status', caption: 'Статус', size: 10, voc: data.vc_gis_status},
                {field: 'err_text', caption: 'Ошибка', size: 10}
            ],
            
            url: '/mosgis/_rest/?type=voc_overhaul_work_types',

            onAdd: $_DO.create_vc_oh_wk_types,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,
            
            onDblClick: function (e) {
                //openTab ('/vc_oh_wk_types/' + e.recid)
            },

        }).refresh ();

    }

})