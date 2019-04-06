define ([], function () {

    var b = ['delete', 'undelete']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['vc_persons_grid']

        var t = g.toolbar

        var sel = g.getSelection ()

        if (sel.length != 1 || g.get (sel [0]).is_deleted) {
            t.disable ('edit', 'delete')
        }
        else {
            t.enable ('edit', 'delete')
        }        

    })}

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({ 

            name: 'vc_persons_grid',

            show: {
                toolbar: true,
                toolbarAdd: !$_USER.role.admin,
                footer: true,
            },

            searches: [            
                {field: 'label_uc',  caption: 'ФИО',  type: 'text'},                              
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},                
                {field: 'uuid_org', caption: 'Организации', type: 'enum', options: {items: data.vc_orgs.items}, off: !$_USER.role.admin},
            ].filter (not_off),

            columns: [                
                {field: 'org.label', caption: 'Организация', size: 70, off: !$_USER.role.admin},
                {field: 'surname', caption: 'Фамилия', size: 50},
                {field: 'firstname', caption: 'Имя', size: 50},
                {field: 'patronymic', caption: 'Отчество', size: 50},
                {field: 'is_female', caption: 'Пол', size: 20, voc: {0: "Мужской", 1: "Женский"}},
                {field: 'placebirth', caption: 'Место рождения', size: 20, hidden: 1},
                {field: 'birthdate', caption: 'Дата рождения', size: 18, render: _dt},
                {field: 'snils', caption: 'СНИЛС', size: 20, hidden: 1},
                {field: 'code_vc_nsi_95',  caption: 'Код документа',     size: 20, voc: data.vc_nsi_95},
                {field: 'series', caption: 'Серия документа', size: 20, hidden: 1},
                {field: 'number_', caption: 'Номер документа', size: 20, hidden: 1},
                {field: 'issuedate', caption: 'Дата выдачи документа', size: 18, render: _dt, hidden: 1},
                {field: 'issuer', caption: 'Кем выдан документ', size: 20, hidden: 1},
            ].filter (not_off),
            
            url: '/_back/?type=vc_persons',
                        
            onAdd:      $_DO.create_vc_persons,
            
            onDblClick: function (e) {
                openTab ('/vc_person/' + e.recid)
            },
            
            onRefresh: function (e) {e.done (color_data_mandatory)},
            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})