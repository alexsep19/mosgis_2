define ([], function () {

    var form_name = 'house_voting_protocol_form'

    $_DO.cancel_house_voting_protocol_form = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'houses'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_house_voting_protocol = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_house_voting_protocol = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return

        var d = w2ui [form_name].values ()
        
        if (!d.protocoldate) die ('protocoldate', 'Пожалуйста, введите дату составления протокола')
        if (!d.votingtype) die ('votingtype', 'Пожалуйста, укажите вид собрания')
        if (!d.votingform) {
            die ('votingform', 'Пожалуйста, выберите форму собрания')
        }

        w2ui [form_name].lock ();

        query ({type: 'houses', action: 'update'}, {data: d}, function (data) {
            
            location.reload ()
       
        })
        
    }

    $_DO.choose_tab_house_voting_protocol = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('house_voting_protocol.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = $('body').data ('data')
      
        done(data)

    }

})