define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'overhaul_regional_program_docs_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: $_USER.role.nsi_20_7 || $_USER.role.admin,
                footer: true,
            },

            searches: [
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
                {field: 'fullname', caption: 'Наименование', type: 'text'},
            ],

            columns: [
                {field: 'code_nsi_79', caption: 'Вид документа', size: 10, voc: data.vc_nsi_79},
                {field: '_number', caption: 'Номер', size: 10},
                {field: '_date', caption: 'Дата', size: 10, render: _dt},
                {field: 'fullname', caption: 'Наименование', size: 10},
                {field: 'legislature', caption: 'Орган власти', size: 10},
            ],
            
            url: '/mosgis/_rest/?type=overhaul_regional_program_documents',

            onAdd: $_DO.create_overhaul_regional_program_doc,
            
            onDblClick: function (e) {
                openTab ('/overhaul_regional_program_doc/' + e.recid)
            },

        }).refresh ();

    }

})