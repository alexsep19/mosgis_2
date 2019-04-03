define ([], function () {

    var b = ['delete', 'undelete']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['msp_decision_bases_grid']

        var t = g.toolbar

        t.disable (b [0])
//        t.disable (b [1])
        
        if (g.getSelection ().length != 1) return

        t.enable (b [g.get (g.getSelection () [0]).is_deleted])

    })}

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({ 

            name: 'msp_decision_bases_grid',

            show: {
                toolbar: true,
                toolbarAdd: !$_USER.role.admin,
                toolbarEdit: !$_USER.role.admin,
                footer: true,
            },     

            toolbar: {
            
                items: [
                    {type: 'button', id: b [0], caption: 'Удалить', onClick: $_DO.delete_msp_decision_bases, disabled: true, off: $_USER.role.admin},
//                    {type: 'button', id: b [1], caption: 'Восстановить', onClick: $_DO.undelete_msp_decision_bases, disabled: true, off: $_USER.role.admin},
                ].filter (not_off),
                
            }, 

            searches: [            
                {field: 'label_uc',  caption: 'Наименование',  type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
                {field: 'id_ctr_status', caption: 'Статус синхронизации',     type: 'enum', options: {items: data.vc_gis_status.items}},
            ].filter (not_off),

            columns: [      
            
                {field: 'org.label', caption: 'Организация', size: 100, off: !$_USER.role.admin},
                {field: 'uniquenumber', caption: 'Код', size: 10},
                {field: 'decisionname', caption: 'Наименование', size: 50},
                {field: 'code_vc_nsi_301',  caption: 'Тип решения', size: 25, voc: data.vc_nsi_301},
                {field: 'id_ctr_status',  caption: 'Статус', size: 50, voc: data.vc_gis_status},


            ].filter (not_off),
            
            url: '/mosgis/_rest/?type=msp_decision_bases',
            
            postData: {data: {uuid_org: $_USER.uuid_org}},
                        
            onAdd:      $_DO.create_msp_decision_bases,            
            onEdit:     $_DO.edit_msp_decision_bases,            
            onDblClick: function (e) {openTab ('/msp_decision_bases/' + e.recid)},
            onRefresh: function (e) {e.done (color_data_mandatory)},            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})